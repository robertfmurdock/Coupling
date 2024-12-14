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

    const xMinMillis = Math.min(...xValues)
    const xMaxMillis = Math.max(...xValues)

    function calculatePrecision() {
        const range = xMaxMillis - xMinMillis
        const hasMinutes = (range / (1000 * 60)) > 1
        const hasHours = (range / (1000 * 60 * 60)) > 1
        const hasDays = (range / (1000 * 60 * 60 * 24)) > 1
        const hasMonths = (range / (1000 * 60 * 60 * 24 * 30)) > 1

        if (hasMonths)
            return {format: '%y-%m-%d'}
        if (hasDays)
            return {format: '%m-%d'}
        else if (hasHours)
            return {format: '%H:%M'}
        else if (hasMinutes)
            return {format: '%H:%M:%S'}
        else
            return {format: '%H:%M:%S.%L'}
    }

    const {format} = calculatePrecision()

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
                <Tooltip
                    labelFormatter={(value) => d3TimeFormat(format)(value)}
                    content={props.tooltip ? (args) => props.tooltip(args) : undefined}
                />
                <Legend
                    width={'90%'}
                    height={'7%'}
                    wrapperStyle={{whiteSpace: 'pre-wrap'}}
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
}
