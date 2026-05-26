const express = require("express");
const fs = require("fs");
const axios = require("axios");
const crypto = require("crypto");

const path = require("path");

const app = express();

app.use(express.json());
app.use(express.static(__dirname));

const HOST = "visual.volcengineapi.com";
const REGION = "cn-north-1";
const SERVICE = "cv";
const VERSION = "2022-08-31";

const REQ_KEY = "jimeng_t2i_v30";
const OUTPUT_DIR = path.join(__dirname, "output");

const keyText = fs.readFileSync(
    __dirname + "/AccessKey.key",
    "utf8"
);

const AK = keyText.match(
    /AccessKeyId:\s*(.*)/
)[1].trim();

const SK = keyText.match(
    /SecretAccessKey:\s*(.*)/
)[1].trim();

function hmacSha256(key, data) {
    return crypto.createHmac("sha256", key).update(data).digest();
}

function sha256Hash(data) {
    return crypto.createHash("sha256").update(data).digest("hex");
}

function uriEscape(str) {
    return encodeURIComponent(str)
        .replace(/!/g, "%21")
        .replace(/'/g, "%27")
        .replace(/\(/g, "%28")
        .replace(/\)/g, "%29")
        .replace(/\*/g, "%2A");
}

function buildCanonicalQueryString(params) {
    return Object.keys(params)
        .sort()
        .map(key => `${uriEscape(key)}=${uriEscape(params[key])}`)
        .join("&");
}

function signV4Request(method, pathname, query, body) {
    const now = new Date();
    const xDate = now.toISOString().replace(/[:\-]|\.\d{3}/g, "");
    const shortDate = xDate.substring(0, 8);
    
    const bodyStr = JSON.stringify(body);
    const payloadHash = sha256Hash(bodyStr);
    
    const canonicalHeaders = [
        `content-type:application/json`,
        `host:${HOST}`,
        `x-content-sha256:${payloadHash}`,
        `x-date:${xDate}`
    ].join("\n");
    
    const signedHeaders = "content-type;host;x-content-sha256;x-date";
    
    const canonicalRequest = [
        method,
        pathname,
        buildCanonicalQueryString(query),
        canonicalHeaders + "\n",
        signedHeaders,
        payloadHash
    ].join("\n");
    
    const credentialScope = `${shortDate}/${REGION}/${SERVICE}/request`;
    const stringToSign = [
        "HMAC-SHA256",
        xDate,
        credentialScope,
        sha256Hash(canonicalRequest)
    ].join("\n");
    
    const kDate = hmacSha256(SK, shortDate);
    const kRegion = hmacSha256(kDate, REGION);
    const kService = hmacSha256(kRegion, SERVICE);
    const kSigning = hmacSha256(kService, "request");
    const signature = hmacSha256(kSigning, stringToSign).toString("hex");
    
    const authorization = `HMAC-SHA256 Credential=${AK}/${credentialScope}, SignedHeaders=${signedHeaders}, Signature=${signature}`;
    
    return {
        "Content-Type": "application/json",
        "Host": HOST,
        "X-Date": xDate,
        "X-Content-Sha256": payloadHash,
        "Authorization": authorization
    };
}

async function signedPost(action, body){
    const query = {
        Action: action,
        Version: VERSION
    };
    
    const headers = signV4Request("POST", "/", query, body);
    
    const res = await axios.post(
        `https://${HOST}/`,
        body,
        {
            params: query,
            headers: headers
        }
    );
    
    return res.data;
}

async function sleep(ms){
    return new Promise(r=>setTimeout(r,ms));
}

app.post("/generate", async(req,res)=>{

    try{

        const prompt = req.body.prompt;

        console.log("prompt:", prompt);

        const submitBody = {
            req_key: REQ_KEY,
            prompt: prompt,
            seed: -1,
            width: 1024,
            height: 1024
        };

        const submit = await signedPost(
            "CVSync2AsyncSubmitTask",
            submitBody
        );

        console.log("submit response:", JSON.stringify(submit, null, 2));

        if(submit.code !== 10000){
            return res.json({
                success:false,
                error: submit.message || JSON.stringify(submit)
            });
        }

        if(!submit.data || !submit.data.task_id){
            return res.json({
                success:false,
                error: "task_id not found in response"
            });
        }

        const taskId = submit.data.task_id;

        console.log("task_id:", taskId);

        let result = null;

        for(let i=0;i<30;i++){

            await sleep(3000);

            const queryBody = {
                req_key: REQ_KEY,
                task_id: taskId,
                req_json: JSON.stringify({ return_url: true })
            };

            result = await signedPost(
                "CVSync2AsyncGetResult",
                queryBody
            );

            console.log("query response:", JSON.stringify(result, null, 2));

            if(result.code !== 10000){
                console.log("query error:", result.message);
                continue;
            }

            if(result.data && result.data.status === "done"){
                break;
            }

        }

        if(!result || result.code !== 10000){
            return res.json({
                success:false,
                error: "API request failed"
            });
        }

        if(!result.data || result.data.status !== "done"){
            return res.json({
                success:false,
                error: "生成超时"
            });
        }

        if(!result.data.image_urls || result.data.image_urls.length === 0){
            return res.json({
                success:false,
                error: "image_urls is empty"
            });
        }

        const imageUrl = result.data.image_urls[0];
        
        if(!fs.existsSync(OUTPUT_DIR)){
            fs.mkdirSync(OUTPUT_DIR, { recursive: true });
        }
        
        const timestamp = Date.now();
        const fileName = `image_${timestamp}.jpg`;
        const localPath = path.join(OUTPUT_DIR, fileName);
        
        console.log("Downloading image from:", imageUrl);
        const imageResponse = await axios.get(imageUrl, { responseType: "arraybuffer" });
        fs.writeFileSync(localPath, imageResponse.data);
        console.log("Image saved to:", localPath);

        res.json({
            success:true,
            url: `/output/${fileName}`,
            localPath: localPath
        });

    }catch(e){

        console.error("Error:", e);

        res.json({
            success:false,
            error:e.message
        });

    }

});

app.listen(3002, ()=>{
    console.log("Server started at http://localhost:3002");
}).on('error', (err)=>{
    console.error("Server error:", err);
});