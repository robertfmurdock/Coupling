FROM node:16-alpine

WORKDIR /usr/src/app/server
COPY package.json serverless.yml /usr/src/app/server/
COPY deploy /usr/src/app/server/deploy
RUN npm install
ENV \
  PUBLIC_HOST=web

ARG STAGE="dev"
ENV STAGE $STAGE

CMD ["npx", "serverless", "package", "--package", "build/lambda-dist", "--stage", "$STAGE"]
