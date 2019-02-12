import CouplingDataService from "../lib/CouplingDataService";
// @ts-ignore
import * as server from "server";

const commandDispatcher = server.com.zegreatrob.coupling.server.commandDispatcher;

const config = require('../config/config');

export default function (userDataService, couplingDataService) {
    const tempDataService = new CouplingDataService(config.tempMongoUrl);
    return function (request, response, next) {

        request.statsdKey = ['http', request.method.toLowerCase(), request.path].join('.');
        if (!request.isAuthenticated()) {
            if (request.originalUrl.includes('.websocket')) {
                request.close();
            } else {
                response.sendStatus(401);
            }
        } else {
            request.userDataService = userDataService;
            let email = request.user.email;
            const tempSuffixIndex = email.indexOf('._temp');
            if (tempSuffixIndex != -1) {
                request.dataService = tempDataService;
                email = email.substring(0, tempSuffixIndex);
            } else {
                request.dataService = couplingDataService;
            }

            request.commandDispatcher = commandDispatcher(request.dataService, email, request.user.tribes);

            next();
        }
    };
};