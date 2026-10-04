// Main JavaScript file for Digital Store

// Utility functions
const Utils = {
    showNotification: function(message, type = 'success') {
        const notification = document.createElement('div');
        notification.className = `alert alert-${type} notification fade-in`;
        notification.innerHTML = `
            <div class="d-flex align-items-center">
                <i class="bi bi-${type === 'success' ? 'check-circle' : 'exclamation-triangle'} me-2"></i>
                <span>${message}</span>
                <button type="button" class="btn-close ms-auto" onclick="this.parentElement.parentElement.remove()"></button>
            </div>
        `;
        document.body.appendChild(notification);
        
        setTimeout(() => {
            notification.style.animation = 'slideOutRight 0.3s ease';
            setTimeout(() => notification.remove(), 300);
        }, 3000);
    },
    
    formatPrice: function(price) {
        return new Intl.NumberFormat('ru-RU', {
            style: 'currency',
            currency: 'RUB',
            minimumFractionDigits: 0
        }).format(price);
    },
    
    debounce: function(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }
};

// Cart functionality
const Cart = {
    addToCart: async function(productId, quantity = 1, event) {
        // Get button from event or find it by productId
        let btn;
        if (event && event.target) {
            btn = event.target.closest('button');
        } else {
            btn = document.querySelector(`button[onclick*="Cart.addToCart(${productId}"]`);
        }
        
        if (!btn) {
            console.error('Button not found');
            return;
        }
        
        const originalText = btn.innerHTML;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span> Добавление...';
        btn.disabled = true;
        
        try {
            const formData = new FormData();
            formData.append('productId', productId);
            formData.append('quantity', quantity);
            
            const response = await fetch('/api/cart/add', {
                method: 'POST',
                body: formData,
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                }
            });
            
            const data = await response.json();
            
            if (response.ok && data.success) {
                Utils.showNotification(data.message || 'Товар добавлен в корзину!', 'success');
                this.updateCartBadge();
                // Animate button
                btn.innerHTML = '<i class="bi bi-check-circle"></i> Добавлено!';
                setTimeout(() => {
                    btn.innerHTML = originalText;
                    btn.disabled = false;
                }, 2000);
            } else {
                throw new Error(data.message || 'Ошибка при добавлении товара');
            }
        } catch (error) {
            Utils.showNotification(error.message || 'Ошибка при добавлении товара в корзину', 'danger');
            btn.innerHTML = originalText;
            btn.disabled = false;
        }
    },
    
    updateQuantity: async function(cartItemId, quantity) {
        const input = document.getElementById(`quantity-${cartItemId}`);
        if (!input) return;
        
        const originalValue = input.value;
        const quantityNum = parseInt(quantity);
        const maxValue = parseInt(input.getAttribute('data-max')) || parseInt(input.max) || 999;
        
        if (isNaN(quantityNum) || quantityNum < 1) {
            input.value = originalValue;
            return;
        }
        
        if (quantityNum > maxValue) {
            input.value = maxValue;
            Utils.showNotification(`Максимальное количество: ${maxValue}`, 'warning');
            return;
        }
        
        if (quantityNum <= 0) {
            if (!confirm('Удалить товар из корзины?')) {
                input.value = originalValue;
                return;
            }
        }
        
        try {
            const formData = new FormData();
            formData.append('cartItemId', cartItemId);
            formData.append('quantity', quantityNum);
            
            const response = await fetch('/cart/update', {
                method: 'POST',
                body: formData,
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                }
            });
            
            if (response.ok) {
                const data = await response.json();
                if (data.success) {
                    Utils.showNotification('Корзина обновлена', 'success');
                    setTimeout(() => location.reload(), 500);
                } else {
                    Utils.showNotification(data.message || 'Ошибка при обновлении корзины', 'danger');
                    input.value = originalValue;
                }
            } else {
                throw new Error('Ошибка сервера');
            }
        } catch (error) {
            Utils.showNotification('Ошибка при обновлении корзины', 'danger');
            input.value = originalValue;
        }
    },
    
    increaseQuantity: function(cartItemId, maxStock) {
        const input = document.getElementById(`quantity-${cartItemId}`);
        if (!input) return;
        
        const currentValue = parseInt(input.value) || 1;
        const newValue = Math.min(maxStock, currentValue + 1);
        input.value = newValue;
        this.updateQuantity(cartItemId, newValue);
    },
    
    decreaseQuantity: function(cartItemId, maxStock) {
        const input = document.getElementById(`quantity-${cartItemId}`);
        if (!input) return;
        
        const currentValue = parseInt(input.value) || 1;
        const newValue = Math.max(1, currentValue - 1);
        input.value = newValue;
        this.updateQuantity(cartItemId, newValue);
    },
    
    removeFromCart: async function(cartItemId) {
        if (!confirm('Удалить товар из корзины?')) return;
        
        const form = event.target.closest('form');
        const row = form.closest('tr');
        
        try {
            const formData = new FormData(form);
            
            const response = await fetch('/cart/remove', {
                method: 'POST',
                body: formData,
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                }
            });
            
            if (response.ok) {
                row.style.animation = 'fadeOut 0.3s ease';
                setTimeout(() => {
                    row.remove();
                    this.updateCartBadge();
                    Utils.showNotification('Товар удален из корзины', 'success');
                    // Reload if cart is empty
                    if (document.querySelectorAll('tbody tr').length === 0) {
                        setTimeout(() => location.reload(), 500);
                    }
                }, 300);
            }
        } catch (error) {
            Utils.showNotification('Ошибка при удалении товара', 'danger');
        }
    },
    
    updateCartBadge: async function() {
        try {
            const response = await fetch('/api/cart/count');
            if (response.ok) {
                const data = await response.json();
                const badge = document.querySelector('.cart-badge');
                if (badge) {
                    badge.textContent = data.count;
                    if (data.count > 0) {
                        badge.style.display = 'flex';
                        badge.classList.add('pulse');
                    } else {
                        badge.style.display = 'none';
                    }
                }
            }
        } catch (error) {
            console.error('Error updating cart badge:', error);
        }
    }
};

// Search functionality
const Search = {
    init: function() {
        const searchInput = document.querySelector('input[name="search"]');
        if (searchInput) {
            const debouncedSearch = Utils.debounce(this.performSearch.bind(this), 300);
            searchInput.addEventListener('input', debouncedSearch);
        }
    },
    
    performSearch: async function(event) {
        const query = event.target.value;
        if (query.length < 2) return;
        
        try {
            const response = await fetch(`/api/products/search?q=${encodeURIComponent(query)}`);
            if (response.ok) {
                const products = await response.json();
                this.displayResults(products);
            }
        } catch (error) {
            console.error('Search error:', error);
        }
    },
    
    displayResults: function(products) {
        // This would update a search results dropdown
        // Implementation depends on UI requirements
    }
};

// Product filters
const Filters = {
    apply: function() {
        const form = document.querySelector('#filter-form');
        if (form) {
            form.submit();
        }
    },
    
    clear: function() {
        window.location.href = '/products';
    }
};

// Load favorites script if available
if (typeof Favorites !== 'undefined') {
    // Favorites functionality is available
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    // Initialize search
    Search.init();
    
    // Update cart badge
    Cart.updateCartBadge();
    
    // Add smooth scroll
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                target.scrollIntoView({ behavior: 'smooth', block: 'start' });
            }
        });
    });
    
    // Add loading states to forms
    document.querySelectorAll('form').forEach(form => {
        form.addEventListener('submit', function() {
            const submitBtn = form.querySelector('button[type="submit"]');
            if (submitBtn && !submitBtn.disabled) {
                submitBtn.disabled = true;
                submitBtn.innerHTML = '<span class="loading"></span> Обработка...';
            }
        });
    });
    
    // Animate cards on scroll
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };
    
    const observer = new IntersectionObserver(function(entries) {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('fade-in');
                observer.unobserve(entry.target);
            }
        });
    }, observerOptions);
    
    document.querySelectorAll('.product-card, .card').forEach(card => {
        observer.observe(card);
    });
});

// Add CSS animation for fadeOut (use unique variable name to avoid conflicts)
const mainStyle = document.createElement('style');
mainStyle.textContent = `
    @keyframes fadeOut {
        from { opacity: 1; transform: scale(1); }
        to { opacity: 0; transform: scale(0.8); }
    }
    @keyframes slideOutRight {
        from { opacity: 1; transform: translateX(0); }
        to { opacity: 0; transform: translateX(100%); }
    }
`;
document.head.appendChild(mainStyle);
