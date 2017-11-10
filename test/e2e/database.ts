import * as monk from "monk";

const config = require("../../config");

export const database = monk.default(config.tempMongoUrl);

export const usersCollection = monk.default(config.mongoUrl).get('users');

export const tribeCollection = database.get('tribes');
export const playersCollection = database.get('players');