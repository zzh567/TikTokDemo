在GoServer目录运行go run server.go在本机8080端口启动本地视频服务器，在Android Studio中打开工程并运行即可成功运行demo。

由于demo运行在Android Studio的虚拟机中，所以代码里是写死从http://10.0.2.2:8080/ 获取数据的，若将apk安装到真机或其他模拟器则无法收到本地Go服务器提供的视频数据。
