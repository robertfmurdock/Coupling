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
    <meta name="referrer" content="strict-origin-when-cross-origin">
    <script src="https://unpkg.com/react@18.3.1/umd/react.production.min.js"
            integrity="sha384-DGyLxAyjq0f9SPpVevD6IgztCFlnMF6oW/XQGmfe+IsZ8TqEiDrcHkMLKI6fiB/Z"
            crossorigin="anonymous"
            referrerpolicy="no-referrer"></script>
    <script src="https://unpkg.com/react-dom@18.3.1/umd/react-dom.production.min.js"
            integrity="sha384-gTGxhz21lVGYNMcdJOyq01Edg0jhn/c22nsx0kyqP0TxaV5WVdsSH1fSDUf5YJj1"
            crossorigin="anonymous"
            referrerpolicy="no-referrer"></script>
    <link href="https://cdn.jsdelivr.net/npm/graphiql@3.8.3/graphiql.min.css" rel="stylesheet"
          integrity="sha384-Mq3vbRBY71jfjQAt/DcjxUIYY33ksal4cgdRt9U/hNPvHBCaT2JfJ/PTRiPKf0aM"
          crossorigin="anonymous"
          referrerpolicy="no-referrer"/>
    <script src="https://cdn.jsdelivr.net/npm/graphiql@3.8.3/graphiql.min.js"
            integrity="sha384-HbRVEFG0JGJZeAHCJ9Xm2+tpknBQ7QZmNlO/DgZtkZ0aJSypT96YYGRNod99l9Ie"
            referrerpolicy="no-referrer"
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
