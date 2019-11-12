import {handleRequest} from "./route-helper";

export default handleRequest(
    (commandDispatcher, request, response) => commandDispatcher.performProposeNewPairsCommand(request, response),
    () => {
    }
);
