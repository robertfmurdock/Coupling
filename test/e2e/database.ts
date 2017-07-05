import * as monk from "monk";

const config = require("../../config");

export const database = monk(config.tempMongoUrl);

export const usersCollection = monk(config.mongoUrl).get('users');

export const tribeCollection = database.get('tribes');
export const playersCollection = database.get('players');