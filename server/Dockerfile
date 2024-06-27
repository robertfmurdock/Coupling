FROM ghcr.io/robertfmurdock/coupling-serverless-base@sha256:bd9fd6d7374b45f34e7b49dabdaa2af84be6a2849ade0d6f7fff7e60886d49f9

RUN npm install abort-controller
COPY executable /serverless/build/executable
COPY serverless.yml /serverless/
COPY deploy /serverless/deploy

CMD node_modules/.bin/serverless offline --config serverless.yml start
