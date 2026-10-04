// Filters functionality for products page
const Filters = {
    init: function() {
        const filtersForm = document.getElementById('filtersForm');
        if (!filtersForm) return;
        
        // Add event listeners for real-time validation
        const minPriceInput = filtersForm.querySelector('input[name="minPrice"]');
        const maxPriceInput = filtersForm.querySelector('input[name="maxPrice"]');
        
        if (minPriceInput && maxPriceInput) {
            minPriceInput.addEventListener('blur', () => this.validatePriceRange());
            maxPriceInput.addEventListener('blur', () => this.validatePriceRange());
        }
        
        // Optional: AJAX form submission (can be enabled if needed)
        // filtersForm.addEventListener('submit', (e) => {
        //     e.preventDefault();
        //     this.submitFiltersAjax(filtersForm);
        // });
    },
    
    validatePriceRange: function() {
        const minPriceInput = document.querySelector('input[name="minPrice"]');
        const maxPriceInput = document.querySelector('input[name="maxPrice"]');
        
        if (!minPriceInput || !maxPriceInput) return;
        
        const minPrice = parseFloat(minPriceInput.value);
        const maxPrice = parseFloat(maxPriceInput.value);
        
        if (minPrice && maxPrice && minPrice > maxPrice) {
            Utils.showNotification('Минимальная цена не может быть больше максимальной', 'warning');
            minPriceInput.focus();
            return false;
        }
        
        if (minPrice < 0) {
            Utils.showNotification('Цена не может быть отрицательной', 'warning');
            minPriceInput.focus();
            return false;
        }
        
        if (maxPrice < 0) {
            Utils.showNotification('Цена не может быть отрицательной', 'warning');
            maxPriceInput.focus();
            return false;
        }
        
        return true;
    },
    
    submitFiltersAjax: async function(form) {
        if (!this.validatePriceRange()) {
            return;
        }
        
        const formData = new FormData(form);
        const params = new URLSearchParams();
        
        // Convert FormData to URL params
        for (const [key, value] of formData.entries()) {
            if (value) {
                params.append(key, value);
            }
        }
        
        try {
            const response = await fetch(`/products?${params.toString()}`, {
                method: 'GET',
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                }
            });
            
            if (response.ok) {
                const html = await response.text();
                const parser = new DOMParser();
                const doc = parser.parseFromString(html, 'text/html');
                
                // Update products grid
                const newProductsGrid = doc.querySelector('.row.g-4');
                const currentProductsGrid = document.querySelector('.row.g-4');
                
                if (newProductsGrid && currentProductsGrid) {
                    currentProductsGrid.innerHTML = newProductsGrid.innerHTML;
                    
                    // Reinitialize favorite buttons
                    document.querySelectorAll('button[data-product-id]').forEach(btn => {
                        const productId = btn.getAttribute('data-product-id');
                        if (productId) {
                            Favorites.checkStatus(productId);
                        }
                    });
                    
                    // Update results count
                    const newResultsInfo = doc.querySelector('.alert.alert-info');
                    const currentResultsInfo = document.querySelector('.alert.alert-info');
                    if (newResultsInfo && currentResultsInfo) {
                        currentResultsInfo.innerHTML = newResultsInfo.innerHTML;
                    }
                    
                    // Scroll to products
                    currentProductsGrid.scrollIntoView({ behavior: 'smooth', block: 'start' });
                    
                    Utils.showNotification('Фильтры применены', 'success');
                }
            } else {
                Utils.showNotification('Ошибка при применении фильтров', 'danger');
            }
        } catch (error) {
            console.error('Error applying filters:', error);
            Utils.showNotification('Ошибка при применении фильтров', 'danger');
        }
    },
    
    resetFilters: function() {
        const form = document.getElementById('filtersForm');
        if (form) {
            form.reset();
            // Reset hidden fields
            const searchInput = form.querySelector('input[name="search"]');
            const categoryInput = form.querySelector('input[name="category"]');
            if (searchInput) searchInput.value = '';
            if (categoryInput) categoryInput.value = '';
            
            // Submit form to reset URL
            window.location.href = '/products';
        }
    }
};

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    Filters.init();
});
