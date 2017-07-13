FROM zegreatrob/javajsbase:latest

WORKDIR /usr/src/app
COPY ["package.json", "yarn.lock", "/usr/src/app/"]
RUN yarn install

COPY . /usr/src/app

ENV \
  PUBLIC_HOST=web \
  MONGOHQ_URL=mongodb://mongo/Coupling \
  MONGO_CONNECTION=mongodb://mongo \
  KARMA_CHROME_ONLY=true

CMD [ "node", "test/continuous-run.js" ]

EXPOSE 3000 8125
