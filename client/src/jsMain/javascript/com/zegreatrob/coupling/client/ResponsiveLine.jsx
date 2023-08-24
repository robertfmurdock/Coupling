import { ResponsiveLine } from '@nivo/line'

export const MyResponsiveLine = (props) => (
    <ResponsiveLine
        animate
        axisBottom={{
            format: '',
            legend: 'Time',
            legendOffset: -12,
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
        pointSize={10}
        pointColor="#FFFFFF"
        pointBorderWidth={2}
        pointBorderColor={{ from: 'serieColor' }}
        pointLabelYOffset={-12}
        useMesh
        xScale={{
            format: '%Y-%m-%d %H:%M:%S.%L',
            type: 'time',
            useUTC: false
        }}
        yScale={{
            type: 'linear'
        }}
    />
)