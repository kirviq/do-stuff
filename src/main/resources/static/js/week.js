
(function() {
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
