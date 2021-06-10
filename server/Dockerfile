FROM node:16-alpine

WORKDIR /usr/src/app/server
COPY package.json serverless.yml /usr/src/app/server/
COPY deploy /usr/src/app/server/deploy
RUN npm install
ENV \
  PUBLIC_HOST=web \
  SERVER_DIR=/usr/src/app/server
CMD ["npx", "serverless", "package", "--package", "build/lambda-dist", "--stage", "dev"]
