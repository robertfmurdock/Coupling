FROM node:7-slim

RUN apt-get update \
  && apt-get install -y \
  bzip2 \
  libfreetype6 \
  libfontconfig \
  git
RUN cd $(npm root -g)/npm \
  && npm install fs-extra \
  && sed -i -e s/graceful-fs/fs-extra/ -e s/fs.rename/fs.move/ ./lib/utils/rename.js

RUN npm install grunt-cli karma-cli --unsafe-perm

WORKDIR /usr/src/app
COPY ["package.json", "/usr/src/app/"]
RUN npm install

COPY . /usr/src/app
ENV \
  PUBLIC_HOST=web \
  MONGOHQ_URL=mongodb://mongo/Coupling \
  MONGO_CONNECTION=mongodb://mongo \
  HEADLESS=true

CMD [ "node", "test/continuous-run.js" ]

EXPOSE 3000 8125
