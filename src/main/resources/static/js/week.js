
(function() {
	(function handleSwipes() {
		function getCurrentWeeksPast() {
			if (!location.search || location.search.indexOf('weeksPast') == -1) {
				return 0;
			}
			return parseInt(location.search.replace(/^.*weeksPast=(\d+).*/, '$1'));
		}
		let startX, startY;
		let lastX, lastY;
		document.addEventListener('touchstart', event => {
			startX = event.touches[0].clientX;
			startY = event.touches[0].clientY;
		});
		document.addEventListener('touchmove', event => {
			lastX = event.touches[0].clientX;
			lastY = event.touches[0].clientY;
		});
		document.addEventListener('touchend', event => {
			const moveX = lastX - startX;
			const moveY = lastY - startY;
			if (moveY > 50) {
				return;
			}
			if (moveX < -50) {
				const currentWeek = getCurrentWeeksPast();
				if (currentWeek === 0) {
					return;
				}
				location.search = `weeksPast=${currentWeek - 1}`
			}
			if (moveX > 50) {
				location.search = `weeksPast=${getCurrentWeeksPast() + 1}`
			}
		});
	})();

	(function handlePopupForms() {
		function getDataFromButton(el) {
			while (el.tagName != 'BUTTON') {
				el = el.parentNode;
			}
			return new Proxy({}, {
				get(target, property) {
					property = property.replace(/([A-Z])/g, '-$1').toLowerCase();
					return el.getAttribute(`data-${property}`);
				},
				set(target, property, value) {
					property = property.replace(/-[a-z]/g, string => string.substring(1).toUpperCase());
					el.setAttribute(`data-${property}`, value);
				}
			});
		}

		function revealStuff(event) {
			const data = getDataFromButton(event.target);
			let form = document.getElementById(data.showId);
			form.className += ' visible';
			form.querySelector('input:not([type=hidden])').focus();
		}

		function hideForm(event) {
			const form = event.target;
			if (form.tagName != 'FORM') {
				return;
			}
			form.className = form.className.replace(/visible/g, '');
		}

		addEventListener('DOMContentLoaded', () => {
			Array.prototype.forEach.call(document.querySelectorAll('.revealer-button'), button => {
				button.addEventListener('click', revealStuff, false);
			});
			Array.prototype.forEach.call(document.querySelectorAll('.fullscreen-form'), button => {
				button.addEventListener('click', hideForm, false);
			});
		});
	})();

	(function supportInputs() {
		function weightInputInputHandler(event) {
			const input = event.target;
			if (input.value.match(/^\d+\.\d$/)) {
				document.getElementById(input.getAttribute('id').replace(/weight/, 'sugar')).focus();
			}
		}
		function sugarInputInputHandler(event) {
			const input = event.target;
			if (input.value.match(/^\d{3}$/)) {
				document.getElementById(input.getAttribute('id').replace(/sugar/, 'bpsys')).focus();
			}
		}
		function bpsysInputInputHandler(event) {
			const input = event.target;
			if (input.value.match(/^\d{3}$/)) {
				document.getElementById(input.getAttribute('id').replace(/bpsys/, 'bpdia')).focus();
			}
		}
		function bpdiaInputInputHandler(event) {
			const input = event.target;
			if (input.value.match(/^\d{2}$/)) {
				document.getElementById(input.getAttribute('id').replace(/bpdia/, 'pulse')).focus();
			}
		}
		addEventListener('DOMContentLoaded', () => {
			Array.prototype.forEach.call(document.querySelectorAll('input[name=weight]'), input => {
				input.addEventListener('keyup', weightInputInputHandler, false);
			});
			Array.prototype.forEach.call(document.querySelectorAll('input[name=sugar]'), input => {
				input.addEventListener('keyup', sugarInputInputHandler, false);
			});
			Array.prototype.forEach.call(document.querySelectorAll('input[name=bpsys]'), input => {
				input.addEventListener('keyup', bpsysInputInputHandler, false);
			});
			Array.prototype.forEach.call(document.querySelectorAll('input[name=bpdia]'), input => {
				input.addEventListener('keyup', bpdiaInputInputHandler, false);
			});
		}, false);
	})();
})();
