package com.zegreatrob.coupling.client.graphql

import emotion.react.css
import react.ElementType
import react.FC
import react.Props
import react.dom.html.ReactHTML.iframe
import react.useEffectOnce
import react.useRef
import web.cssom.vh
import web.cssom.vw
import web.html.HTMLIFrameElement
import kotlin.js.Json
import kotlin.js.Promise

val GraphiQL: ElementType<GraphiQLProps> = FC { props ->
    val iframeRef = useRef<HTMLIFrameElement>(null)
    useEffectOnce {
        iframeRef.current?.contentWindow?.set("graphiqlProps", props)
    }

    iframe {
        ref = iframeRef
        css {
            width = 100.vw
            height = 100.vh
        }
        name = "graphiql-frame"
        allowFullScreen = true
        sandbox = "allow-scripts allow-same-origin"
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
<body style='margin:0;'>
<div id="root" style='width:100vw; height:100vh'></div>
</body>
<script>
    const root = ReactDOM.createRoot(document.getElementById('root'));
    root.render(
        React.createElement(GraphiQL, window.graphiqlProps)
    );
</script>
</html>
        """.trimIndent()
    }
}

external interface GraphiQLProps : Props {
    var editorTheme: String?
    var fetcher: (graphQlParams: Json) -> Promise<String>
}
