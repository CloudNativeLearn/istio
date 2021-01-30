#基于的基础镜像

FROM python:3.5.3

# 维护者信息

MAINTAINER name  name@163.com

# 代码添加到code文件夹

ADD ./src /code

# 设置code文件夹是工作目录

WORKDIR /code
EXPOSE 7777
CMD ["python", "/code/service.py"]
