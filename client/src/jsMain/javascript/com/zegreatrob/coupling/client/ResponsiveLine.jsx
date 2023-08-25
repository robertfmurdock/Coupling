import {ResponsiveLine} from '@nivo/line'

export const MyResponsiveLine = (props) => {
    function calculatePrecision() {
        const allPoints = props.data.flatMap(line => line.data)
        const xValues = allPoints.map(point => {
            return point.x.getTime()
        });
        const min = Math.min(...xValues)
        const max = Math.max(...xValues)

        const range = max - min

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
                legend: 'Heat',
                legendOffset: 12
            }}
            curve="monotoneX"
            data={props.data}
            margin={{
                bottom: 60,
                left: 80,
                right: 20,
                top: 20
            }}
            pointSize={6}
            pointColor="#FFFFFF"
            pointBorderWidth={1}
            pointBorderColor={{from: 'serieColor'}}
            pointLabelYOffset={-12}
            useMesh
            xScale={{
                format: '%Y-%m-%d %H:%M:%S.%L',
                type: 'time',
                precision: precision,
                useUTC: false
            }}
            yScale={{
                type: 'linear'
            }}
        />
    )
}