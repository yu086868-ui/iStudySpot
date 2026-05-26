const btn = document.getElementById("generateBtn");
const statusDiv = document.getElementById("status");
const img = document.getElementById("resultImg");

btn.onclick = async () => {

    const prompt = document.getElementById("prompt").value;

    statusDiv.innerText = "提交任务中...";

    const res = await fetch("/generate", {
        method: "POST",
        headers: {
            "Content-Type":"application/json"
        },
        body: JSON.stringify({
            prompt
        })
    });

    const data = await res.json();

    if(data.success){
        statusDiv.innerText = "生成完成";
        img.src = data.url;
        img.style.display = "block";
    }else{
        statusDiv.innerText = data.error;
    }
};