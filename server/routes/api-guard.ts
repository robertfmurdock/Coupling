import CouplingDataService from "../lib/CouplingDataService";
// @ts-ignore
import * as server from "Coupling-server";

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
            let email = request.user.email;
            const tempSuffixIndex = email.indexOf('._temp');
            let dataService: CouplingDataService;
            if (tempSuffixIndex != -1) {
                dataService = tempDataService;
            } else {
                dataService = couplingDataService;
            }

            request.commandDispatcher = commandDispatcher(
                dataService,
                userDataService.usersCollection,
                email,
                request.user.tribes,
                `${request.method} ${request.path}`
            );

            next();
        }
    };
};