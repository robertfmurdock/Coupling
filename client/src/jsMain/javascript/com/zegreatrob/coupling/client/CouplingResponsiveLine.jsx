import {ResponsiveLine} from '@nivo/line'
import { ResponsiveHeatMap } from '@nivo/heatmap'

export const CouplingResponsiveLine = (props) => {
    const allPoints = props.data.flatMap(line => line.data)
    const xValues = allPoints.map(point => point.x.getTime());
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

export const CouplingResponsiveHeatMap = ({ data, colors }) => {
    return (
        <ResponsiveHeatMap
            data={data}
            margin={{top: 60, right: 90, bottom: 60, left: 90}}
            valueFormat=">-.2s"
            axisTop={{
                tickSize: 5,
                tickPadding: 5,
                tickRotation: -90,
                legend: '',
                legendOffset: 46,
                truncateTickAt: 0
            }}
            axisRight={{
                tickSize: 5,
                tickPadding: 5,
                tickRotation: 0,
                legend: 'country',
                legendPosition: 'middle',
                legendOffset: 70,
                truncateTickAt: 0
            }}
            axisLeft={{
                tickSize: 5,
                tickPadding: 5,
                tickRotation: 0,
                legend: 'country',
                legendPosition: 'middle',
                legendOffset: -72,
                truncateTickAt: 0
            }}
            colors={colors}
            emptyColor="#555555"
            legends={[
                {
                    anchor: 'bottom',
                    translateX: 0,
                    translateY: 30,
                    length: 400,
                    thickness: 8,
                    direction: 'row',
                    tickPosition: 'after',
                    tickSize: 3,
                    tickSpacing: 4,
                    tickOverlap: false,
                    tickFormat: '>-.2s',
                    title: 'Value →',
                    titleAlign: 'start',
                    titleOffset: 4
                }
            ]}
        />
    );
}