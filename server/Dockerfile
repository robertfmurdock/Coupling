FROM node:16-alpine

WORKDIR /usr/src/app/server
COPY package.json serverless.yml deploy /usr/src/app/server/
COPY deploy /usr/src/app/server
RUN npm install
ENV \
  PUBLIC_HOST=web
CMD ["npx", "serverless", "package", "--package", "build/lambda-dist", "--stage", "dev"]
