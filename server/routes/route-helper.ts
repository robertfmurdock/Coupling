export function handleRequest(commandRunFunction, successFunction) {
    return (request, response) => {

        let commandDispatcher = request.commandDispatcher;
        commandRunFunction(commandDispatcher, request, response)
            .then((data) => successFunction(response, data, request))
            .catch(function (error) {
                console.log(error);
                response.statusCode = 500;
                response.send({message: error.message});
            });
    }
}
