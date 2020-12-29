addEventListener('DOMContentLoaded', () => {
	const min = [weightData && weightData[0][0], sugarData && sugarData[0][0], bpSysData && bpSysData[0][0], bpDiaData && bpDiaData[0][0], pulseData && pulseData[0][0]]
		.filter(val => val !== undefined)
		.reduce((v1, v2) => Math.min(v1, v2));
	const max = [weightData && weightData[weightData.length - 1][0], sugarData && sugarData[sugarData.length - 1][0], bpSysData && bpSysData[bpSysData.length - 1][0], bpDiaData && bpDiaData[bpDiaData.length - 1][0], pulseData && pulseData[pulseData.length - 1][0]]
		.filter(val => val !== undefined)
		.reduce((v1, v2) => Math.max(v1, v2));

	if (weightData) {
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
				min,
				max,
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
	}
	if (sugarData) {
		Highcharts.chart('sugar-chart', {
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
				min,
				max,
				labels: {
					style: {
						color: '#c0c0c0'
					}
				}
			},
			yAxis: {
				title: {
					text: 'mg/dl'
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
				name: 'mg/dl',
				data: sugarData
			}]
		});
	}
	if (bpSysData) {
		Highcharts.chart('bp-chart', {
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
				min,
				max,
				labels: {
					style: {
						color: '#c0c0c0'
					}
				}
			},
			yAxis: {
				title: {
					text: 'Blutdruck (sys/dia) | Herzfrequenz (min\u207B\u00B9)'
				},
				gridLineColor: '#202020',
				labels: {
					style: {
						color: '#c0c0c0'
					}
				}
			},
			tooltip: {
				formatter() {
					const date = Highcharts.dateFormat('%a, %Y-%m-%d %H:%M', this.x);
					return `<b>${date}</b><br/>
							Blutdruck: ${this.points[0].point.y}/${this.points[1].point.y} mmHg<br/>
							Puls: ${this.points[2].point.y} min\u207B\u00B9<br/>`;
				},
				crosshairs: true,
				shared: true
			},
			series: [{
				name: 'Systolisch',
				color: 'red',
				data: bpSysData
			}, {
				name: 'Diastolisch',
				color: 'red',
				data: bpDiaData
			}, {
				name: 'Puls',
				color: 'darkblue',
				data: pulseData
			}]
		});
	}
}, false);
