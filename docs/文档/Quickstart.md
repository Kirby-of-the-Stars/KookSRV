# 简介

> 欢迎使用 KookSRV 插件! 本插件意在给 KOOK 打造一个像[DiscordSRV](https://github.com/DiscordSRV/DiscordSRV)一样的互联插件!  
> A Minecraft to KOOK and back link plugin!  

## 快速开始  

> 首先，你需要到 KOOK 的[开发者平台](https://developer.kookapp.cn/)的应用页新建一个应用，以创建一个"机器人的账号"，  
> 然后点击新建好的机器人，进到机器人的详情页面，这时你可以给机器人账号换好你想要的头像与名字，  
> 然后点击左侧的"**机器人**"选项，确保机器人的连接模式是"**websocket**"模式，并且获取机器人的登陆凭证"**Token**"  
> 点击**邀请链接**，把机器人邀请链接复制到浏览器当中打开，将机器人邀请到你的KOOK服务器内，  
> 这样机器人账号就准备好了。

> 在插件的发布网站或者Github仓库里的[Releases](https://github.com/Kirby-of-the-Stars/KookSRV/releases)下载好你要用的 KookSRV 插件。  
> 将插件放入到服务端的 plugins 文件夹内，启动服务端。  
> 第一次启动本插件会加载失败，因为配置文件里还没有填入KOOK机器人的"**Token**"与要建立互联的"**服务器频道ID**"。  
> 1. 打开服务端的 plugins\KookSRV 文件夹里的 config.yml,将上面提到的"**Token**"填到配置项中的 **bot-token** 的对应位置当中  
> 2. 打开 KOOK 客户端或网页端设置里的高级设置里的开发者模式，然后转到要建立互联的KOOK服务器文字频道，  
> 对着左侧频道栏里的频道右键，可以看见有个复制ID选项，复制这个ID，填到配置项中的 **targetChannelID** 的对应位置当中  
> 
> 做完以上两项工作，再次启动服务端，等待服务器启动完毕后，此时在 KOOK 服务器的用户列表当中看到机器人在线，  
> 说明 KookSRV 插件已经正确配置并且加载成功，此时插件的功能全都可以正常使用。

!> 注意，填写 **config.yml** 时，请严格遵守 **yml** 文件的格式! 如果你是一位经验丰富的服务器服主、技术，那么这点你应该很清楚!
