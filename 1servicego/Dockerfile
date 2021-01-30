FROM docker.io/alpine
ADD ./src/service /go/
RUN chmod 777 /go/service
EXPOSE 80
CMD /go/service