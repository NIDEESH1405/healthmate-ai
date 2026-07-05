(function () {
  const slider = document.getElementById('severity');
  const label = document.getElementById('severityLabel');
  if (slider && label) {
    slider.addEventListener('input', function () {
      label.textContent = this.value;
    });
  }

  // Auto-scroll chat window to the latest message on load.
  const chatWindow = document.querySelector('.chat-window');
  if (chatWindow) {
    chatWindow.scrollTop = chatWindow.scrollHeight;
  }
})();
