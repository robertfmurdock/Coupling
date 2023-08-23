import { ResponsiveLine } from '@nivo/line'


const data = [
    {
        "id": "japan",
        "color": "hsl(340, 70%, 50%)",
        "data": [
            {
                "x": "plane",
                "y": 111
            },
            {
                "x": "helicopter",
                "y": 100
            },
            {
                "x": "boat",
                "y": 262
            },
            {
                "x": "train",
                "y": 114
            },
            {
                "x": "subway",
                "y": 263
            },
            {
                "x": "bus",
                "y": 54
            },
            {
                "x": "car",
                "y": 233
            },
            {
                "x": "moto",
                "y": 228
            },
            {
                "x": "bicycle",
                "y": 156
            },
            {
                "x": "horse",
                "y": 60
            },
            {
                "x": "skateboard",
                "y": 235
            },
            {
                "x": "others",
                "y": 127
            }
        ]
    },
    {
        "id": "france",
        "color": "hsl(358, 70%, 50%)",
        "data": [
            {
                "x": "plane",
                "y": 56
            },
            {
                "x": "helicopter",
                "y": 238
            },
            {
                "x": "boat",
                "y": 200
            },
            {
                "x": "train",
                "y": 242
            },
            {
                "x": "subway",
                "y": 218
            },
            {
                "x": "bus",
                "y": 20
            },
            {
                "x": "car",
                "y": 78
            },
            {
                "x": "moto",
                "y": 144
            },
            {
                "x": "bicycle",
                "y": 281
            },
            {
                "x": "horse",
                "y": 249
            },
            {
                "x": "skateboard",
                "y": 134
            },
            {
                "x": "others",
                "y": 282
            }
        ]
    },
    {
        "id": "us",
        "color": "hsl(172, 70%, 50%)",
        "data": [
            {
                "x": "plane",
                "y": 218
            },
            {
                "x": "helicopter",
                "y": 165
            },
            {
                "x": "boat",
                "y": 48
            },
            {
                "x": "train",
                "y": 109
            },
            {
                "x": "subway",
                "y": 172
            },
            {
                "x": "bus",
                "y": 267
            },
            {
                "x": "car",
                "y": 138
            },
            {
                "x": "moto",
                "y": 272
            },
            {
                "x": "bicycle",
                "y": 169
            },
            {
                "x": "horse",
                "y": 267
            },
            {
                "x": "skateboard",
                "y": 184
            },
            {
                "x": "others",
                "y": 290
            }
        ]
    },
    {
        "id": "germany",
        "color": "hsl(189, 70%, 50%)",
        "data": [
            {
                "x": "plane",
                "y": 294
            },
            {
                "x": "helicopter",
                "y": 195
            },
            {
                "x": "boat",
                "y": 39
            },
            {
                "x": "train",
                "y": 13
            },
            {
                "x": "subway",
                "y": 16
            },
            {
                "x": "bus",
                "y": 183
            },
            {
                "x": "car",
                "y": 101
            },
            {
                "x": "moto",
                "y": 51
            },
            {
                "x": "bicycle",
                "y": 294
            },
            {
                "x": "horse",
                "y": 60
            },
            {
                "x": "skateboard",
                "y": 135
            },
            {
                "x": "others",
                "y": 160
            }
        ]
    },
    {
        "id": "norway",
        "color": "hsl(73, 70%, 50%)",
        "data": [
            {
                "x": "plane",
                "y": 32
            },
            {
                "x": "helicopter",
                "y": 103
            },
            {
                "x": "boat",
                "y": 2
            },
            {
                "x": "train",
                "y": 77
            },
            {
                "x": "subway",
                "y": 216
            },
            {
                "x": "bus",
                "y": 228
            },
            {
                "x": "car",
                "y": 148
            },
            {
                "x": "moto",
                "y": 90
            },
            {
                "x": "bicycle",
                "y": 190
            },
            {
                "x": "horse",
                "y": 246
            },
            {
                "x": "skateboard",
                "y": 190
            },
            {
                "x": "others",
                "y": 165
            }
        ]
    }
]

// make sure parent container have a defined height when using
// responsive component, otherwise height will be 0 and
// no chart will be rendered.
// website examples showcase many properties,
// you'll often use just a few of them.
export const MyResponsiveLine = () => (
    <ResponsiveLine
        data={data}
        margin={{ top: 50, right: 110, bottom: 50, left: 60 }}
        xScale={{ type: 'point' }}
        yScale={{
            type: 'linear',
            min: 'auto',
            max: 'auto',
            stacked: true,
            reverse: false
        }}
        yFormat=" >-.2f"
        axisTop={null}
        axisRight={null}
        axisBottom={{
            tickSize: 5,
            tickPadding: 5,
            tickRotation: 0,
            legend: 'transportation',
            legendOffset: 36,
            legendPosition: 'middle'
        }}
        axisLeft={{
            tickSize: 5,
            tickPadding: 5,
            tickRotation: 0,
            legend: 'count',
            legendOffset: -40,
            legendPosition: 'middle'
        }}
        pointSize={10}
        pointColor={{ theme: 'background' }}
        pointBorderWidth={2}
        pointBorderColor={{ from: 'serieColor' }}
        pointLabelYOffset={-12}
        useMesh={true}
        legends={[
            {
                anchor: 'bottom-right',
                direction: 'column',
                justify: false,
                translateX: 100,
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