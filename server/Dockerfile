FROM ghcr.io/robertfmurdock/coupling-serverless-base:latest

COPY executable /serverless/build/executable
COPY serverless.yml /serverless/
COPY .env /serverless/
COPY deploy /serverless/deploy

CMD node_modules/.bin/serverless offline --config serverless.yml start
