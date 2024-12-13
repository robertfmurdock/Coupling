import {
    LineChart, CartesianGrid,
    XAxis,
    YAxis,
    Tooltip,
    Legend,
    Line,
    ResponsiveContainer,
} from 'recharts'
import {timeFormat as d3TimeFormat} from 'd3-time-format'
import {scaleOrdinal, scaleTime} from 'd3-scale'
import {schemeCategory10} from 'd3-scale-chromatic'
import * as React from "react"

export const CouplingResponsiveLine = (props) => {
    const allPoints = props.data.flatMap(line => [...(line.data.map(value => ({
        ...value,
        x: value.x.getTime(),
        [`${line.id}`]: value.y
    })))]).sort((a, b) => a.x - b.x)

    const flattenedPoints = Object.entries(Object.groupBy(allPoints, item => item.x))
        .map(group => group[1].reduce((result, current) => Object.assign(result, current), {}));
    const lineIds = props.data.map(value => value.id);
    const xValues = allPoints.map(point => point.x);
    const xMin = props.xMin ?? new Date(Math.min(...xValues))
    const xMax = props.xMax ?? new Date(Math.max(...xValues))

    const xMinMillis = Math.min(...xValues)
    const xMaxMillis = Math.max(...xValues)

    function calculatePrecision() {
        const range = xMaxMillis - xMinMillis
        const hasMinutes = (range / (1000 * 60)) > 1
        const hasHours = (range / (1000 * 60 * 60)) > 1
        const hasDays = (range / (1000 * 60 * 60 * 24)) > 1
        const hasMonths = (range / (1000 * 60 * 60 * 24 * 30)) > 1

        if (hasMonths)
            return {format: '%y-%m-%d', precision: "day"}
        if (hasDays)
            return {format: '%m-%d', precision: "hour"}
        else if (hasHours)
            return {format: '%H:%M', precision: "minute"}
        else if (hasMinutes)
            return {format: '%H:%M:%S', precision: "second"}
        else
            return {format: '%H:%M:%S.%L', precision: "millisecond"}
    }

    const {format, precision} = calculatePrecision()

    const myColor = scaleOrdinal().domain(lineIds).range(schemeCategory10);
    const timeScale = scaleTime().domain([xMinMillis, xMaxMillis]).nice();
    const [activeSeries, setActiveSeries] = React.useState([]);
    const handleLegendClick = (dataKey) => {
        if (activeSeries.includes(dataKey)) {
            setActiveSeries(activeSeries.filter(el => el !== dataKey));
        } else {
            setActiveSeries(prev => [...prev, dataKey]);
        }
    };
    return (
        <ResponsiveContainer width="100%" height="100%">
            <LineChart
                data={flattenedPoints}
                margin={{
                    bottom: 60,
                    left: 40,
                    right: 80,
                    top: 20
                }}>
                <CartesianGrid strokeDasharray="3 3"/>
                <XAxis
                    dataKey="x"
                    domain={timeScale.domain().map(date => date.valueOf())}
                    scale={timeScale}
                    type={'number'}
                    ticks={timeScale.ticks(5).map(date => date.valueOf())}
                    tickFormatter={d3TimeFormat(format)}
                    fontSize={12}
                />
                <YAxis
                    dataKey='y'
                    type="number"
                />
                <Tooltip labelFormatter={(value) => d3TimeFormat(format)(value)}/>
                <Legend
                    onClick={props => handleLegendClick(props.dataKey)}
                />
                {lineIds.map((lineId, index) =>
                    <Line
                        connectNulls
                        key={lineId}
                        type="monotone"
                        dataKey={lineId}
                        hide={activeSeries.includes(lineId)}
                        stroke={myColor(index)}
                        labelFormatter={(value) => d3TimeFormat(format)(value)}
                    />)}
            </LineChart>
        </ResponsiveContainer>
    )

    return (<ResponsiveLine
            animate
            axisBottom={{
                format: format,
                legend: 'Time',
                legendOffset: -12,
                tickOffset: -20,
                tickRotation: 85,
            }}
            axisLeft={{
                legend: props.legend,
                legendOffset: 12
            }}
            colors={{scheme: 'paired'}}
            curve="monotoneX"
            data={props.data}
            margin={{
                bottom: 60,
                left: 40,
                right: 80,
                top: 20
            }}
            pointSize={6}
            pointColor="#FFFFFF"
            pointBorderWidth={1}
            pointBorderColor={{from: 'serieColor'}}
            pointLabelYOffset={-12}
            useMesh
            xScale={{
                format: format,
                type: 'time',
                precision: precision,
                useUTC: false,
                min: xMin,
                max: xMax,
            }}
            xFormat={"time:" + format}
            yScale={{
                type: 'linear'
            }}
            tooltip={props.tooltip ? (args) => props.tooltip(args.point.data) : undefined}
            legends={[
                {
                    anchor: 'bottom-right',
                    direction: 'column',
                    justify: false,
                    translateX: 85,
                    translateY: 0,
                    itemsSpacing: 0,
                    itemDirection: 'left-to-right',
                    itemWidth: 80,
                    itemHeight: 20,
                    itemOpacity: 0.75,
                    symbolSize: 12,
                    symbolShape: 'circle',
                    symbolBorderColor: 'rgba(0, 0, 0, .5)',
                    effects: [
                        {
                            on: 'hover',
                            style: {
                                itemBackground: 'rgba(0, 0, 0, .03)',
                                itemOpacity: 1
                            }
                        }
                    ]
                }
            ]}
        />
    )
}
