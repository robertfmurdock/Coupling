FROM ghcr.io/robertfmurdock/coupling-serverless-base

COPY executable /serverless/build/executable
COPY serverless.yml /serverless/
COPY deploy /serverless/deploy

CMD node_modules/.bin/serverless offline --config serverless.yml start
