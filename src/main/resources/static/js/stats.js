if (weightData) {
	addEventListener('DOMContentLoaded', () => {
		Highcharts.chart('weight-chart', {
			chart: {
				zoomType: 'x',
				backgroundColor: null
			},
			title: {
				style: {
					display: 'none'
				}
			},
			xAxis: {
				type: 'datetime',
				labels: {
					style: {
						color: '#c0c0c0'
					}
				}
			},
			yAxis: {
				title: {
					text: 'kg'
				},
				gridLineColor: '#202020',
				labels: {
					style: {
						color: '#c0c0c0'
					}
				}
			},
			legend: {
				enabled: false
			},
			plotOptions: {
				line: {
					color: 'red',
					width: 3
				// },
				// series: {
				// 	pointStart: 0
				}
			},

			series: [{
				type: 'line',
				name: 'kg',
				data: weightData
			}]
		});
	}, false);
}
