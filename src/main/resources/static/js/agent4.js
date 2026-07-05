(function () {
  const select = document.getElementById('medicineSelect');
  const nameInput = document.getElementById('medicineName');
  if (select && nameInput) {
    select.addEventListener('change', function () {
      if (this.value) {
        nameInput.value = this.value;
      }
    });
  }
})();
