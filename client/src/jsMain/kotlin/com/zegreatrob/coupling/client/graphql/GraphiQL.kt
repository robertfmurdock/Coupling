package com.zegreatrob.coupling.client.graphql

import react.ElementType
import react.FC
import react.Props
import react.dom.html.ReactHTML.iframe

val GraphiQL: ElementType<GraphiQLProps> = FC { props ->
    iframe {
        name = "graphiql-frame"
        allowFullScreen = true
        sandbox = "allow-scripts allow-same-origin"
        asDynamic()["height"] = "100%"
        asDynamic()["width"] = "100%"
        
        this.srcDoc = """
<!DOCTYPE html>
<html width="100%" height="100%">
<head>
    <script crossorigin src="https://unpkg.com/react@18/umd/react.production.min.js"></script>
    <script crossorigin src="https://unpkg.com/react-dom@18/umd/react-dom.production.min.js"></script>
    <link href="https://cdn.jsdelivr.net/npm/graphiql/graphiql.min.css" rel="stylesheet"/>
    <script src="https://cdn.jsdelivr.net/npm/graphiql@3.8.3/graphiql.min.js"
            integrity="sha256-IvqrlAZ7aV5feVlhn75obrzIlVACoMl9mGvLukrUvCw="
            crossorigin="anonymous"></script>
</head>
<body>
<div id="root"></div>
</body>
<script>
    const root = ReactDOM.createRoot(document.getElementById('root'));
    root.render(
        React.createElement(GraphiQL, {
            fetcher: (graphQlParams) =>
                fetch('${props.url}', {
                    headers: {
                        "Authorization": "Bearer ${props.token}",
                        "Content-Type": "application/json"
                    },
                    method: "POST",
                    body: JSON.stringify(graphQlParams)
                })
        })
    );
</script>
</html>
        """.trimIndent()
    }
}

external interface GraphiQLProps : Props {
    var editorTheme: String
    var url: String
    var token: String
}
