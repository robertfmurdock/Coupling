FROM node:17-alpine

WORKDIR /serverless

RUN npm install serverless serverless-offline serverless-offline-ssm

COPY build/executable /serverless/build/executable
COPY serverless.yml /serverless/
COPY deploy /serverless/deploy

CMD node_modules/.bin/serverless offline --config serverless.yml start