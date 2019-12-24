
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
			document.getElementById(data.showId).className += ' visible';
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
})();
