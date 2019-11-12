export function handleRequest(commandRunFunction) {
    return (request, response) => {
        let commandDispatcher = request.commandDispatcher;
        commandRunFunction(commandDispatcher, request, response)
            .catch(function (error) {
                console.log(error);
                response.statusCode = 500;
                response.send({message: error.message});
            });
    }
}
