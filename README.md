HDFSUpload
==========
这个项目是我根据网上大牛的文件上传进度展示做了一些改动
主要用于对文件上传到HDFS上进行展示
目前是对文件夹进行上传

配置文件在HDFSProperties.java里，用于配置Hadoop环境
采用监听模式，获取文件夹内文件总数，并实时显示上传个数

项目IDE为eclipse，直接导入便可使用

或者打成war包，部署在web服务器中。

文件上传需要从本地选择文件夹。
