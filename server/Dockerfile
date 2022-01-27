FROM node:17

WORKDIR /serverless

RUN npm install serverless@2.72.2 serverless-offline serverless-offline-ssm

COPY executable /serverless/build/executable
COPY serverless.yml /serverless/
COPY deploy /serverless/deploy

CMD node_modules/.bin/serverless offline --config serverless.yml start