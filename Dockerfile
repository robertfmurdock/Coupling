FROM node:0.12

WORKDIR /usr/src/app

RUN npm install -g grunt-cli karma-cli phantomjs --unsafe-perm
COPY ["package.json", "/usr/src/app/"]
RUN npm install --unsafe-perm
COPY . /usr/src/app
ENV \
  PUBLIC_HOST=web \
  MONGOHQ_URL=mongodb://mongo/Coupling \
  MONGO_CONNECTION=mongodb://mongo \
  PHANTOMJS_BIN=/usr/local/lib/node_modules/phantomjs/lib/phantom/bin/phantomjs

CMD [ "grunt", "dockerserve" ]

EXPOSE 3000
