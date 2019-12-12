
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
	function showForm(event) {
		const data = getDataFromButton(event.target);
		document.getElementById(data.formId).className += ' visible';
	}
	addEventListener('DOMContentLoaded', () => {
		Array.prototype.forEach.call(document.querySelectorAll('.add-column .add-multiple-button'), button => {
			button.addEventListener('click', showForm, false);
		});
	});
})();
