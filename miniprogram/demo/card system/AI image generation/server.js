const express = require("express");
const fs = require("fs");
const axios = require("axios");
const { Signer } = require("@volcengine/openapi");

const app = express();

app.use(express.json());
app.use(express.static("./"));

const HOST = "https://visual.volcengineapi.com";
const REGION = "cn-north-1";
const SERVICE = "cv";
const VERSION = "2022-08-31";

const REQ_KEY = "jimeng_t2i_v30";

// 读取 AK SK
const keyText = fs.readFileSync(
    "./AccessKey.key",
    "utf8"
);

const AK = keyText.match(
    /AccessKeyId:\s*(.*)/
)[1].trim();

const SK = keyText.match(
    /SecretAccessKey:\s*(.*)/
)[1].trim();

async function signedPost(action, body){

    const query = {
        Action: action,
        Version: VERSION
    };

    const request = {
        method: "POST",
        url: HOST,
        path: "/",
        query,
        headers: {
            "Content-Type":"application/json",
            Host:"visual.volcengineapi.com"
        },
        body: JSON.stringify(body),
        region: REGION,
        service: SERVICE
    };

    Signer.sign(request, AK, SK);

    const res = await axios.post(
        HOST + "/",
        body,
        {
            params: query,
            headers: request.headers
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

        // 1 提交任务
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

        console.log(submit);

        if(submit.code !== 10000){

            return res.json({
                success:false,
                error:
                    submit.message ||
                    JSON.stringify(submit)
            });

        }

        const taskId =
            submit.data.task_id;

        console.log(
            "task_id:",
            taskId
        );

        // 2 轮询
        let result = null;

        for(let i=0;i<30;i++){

            await sleep(3000);

            const queryBody = {
                req_key: REQ_KEY,
                task_id: taskId
            };

            result = await signedPost(
                "CVSync2AsyncGetResult",
                queryBody
            );

            console.log(result);

            if(
                result.data &&
                result.data.status ===
                "done"
            ){
                break;
            }

        }

        if(
            !result ||
            !result.data ||
            result.data.status !== "done"
        ){

            return res.json({
                success:false,
                error:"生成超时"
            });

        }

        const url =
            result.data.image_urls[0];

        res.json({
            success:true,
            url
        });

    }catch(e){

        console.error(e);

        res.json({
            success:false,
            error:e.message
        });

    }

});

app.listen(3000,()=>{

    console.log(
        "http://localhost:3000"
    );

});