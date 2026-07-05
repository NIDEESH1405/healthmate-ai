(function () {
  const tabLinks = document.querySelectorAll('.tab-link');
  const panels = {
    qa: document.getElementById('tab-qa'),
    browse: document.getElementById('tab-browse')
  };

  tabLinks.forEach(link => {
    link.addEventListener('click', function () {
      tabLinks.forEach(l => l.classList.remove('active'));
      Object.values(panels).forEach(p => p.classList.remove('active'));
      this.classList.add('active');
      panels[this.dataset.tab].classList.add('active');
    });
  });

  // If a Q&A answer was just returned (flash attribute present), keep the QA tab active (default).
})();
