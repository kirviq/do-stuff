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
if (sugarData) {
	addEventListener('DOMContentLoaded', () => {
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
	}, false);
}
if (bpSysData) {
	addEventListener('DOMContentLoaded', () => {
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
				labels: {
					style: {
						color: '#c0c0c0'
					}
				}
			},
			yAxis: [
				{
					title: {
						text: 'Blutdruck (sys/dia)'
					},
					gridLineColor: '#202020',
					labels: {
						style: {
							color: '#c0c0c0'
						}
					}
				}, {
					title: {
						text: 'Herzfrequenz (min\u207B\u00B9)'
					},
					gridLineColor: '#202020',
					labels: {
						style: {
							color: '#c0c0c0'
						}
					},
					opposite: true
				}],
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
			plotOptions: {
				// series: {
				// 	animation: false,
				// 		marker: {
				// 		radius: 3
				// 	},
				// 	fillopacity: 0.1
				// }
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
	}, false);
}
