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
	function addStuff(event) {
		const data = getDataFromButton(event.target);
		const form = document.getElementById('adder');
		form['type'].value = data.type;
		form['date'].value = data.date;
		form.submit();
	}
	function deleteStuff(event) {
		const data = getDataFromButton(event.target);
		const form = document.getElementById('remover');
		form['id'].value = data.eventId;
		form.submit();
	}
	addEventListener('DOMContentLoaded', () => {
		Array.prototype.forEach.call(document.querySelectorAll('.add-column .do-button'), button => {
			button.addEventListener('click', addStuff, false);
		});
		Array.prototype.forEach.call(document.querySelectorAll('.event-column .do-button'), button => {
			button.addEventListener('click', deleteStuff, false);
		});
	});
})();
