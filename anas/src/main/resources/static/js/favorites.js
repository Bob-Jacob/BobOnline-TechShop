// Favorites functionality
const Favorites = {
    toggle: async function(productId, event) {
        // Get button from event or find it by productId
        let btn;
        if (event && event.target) {
            btn = event.target.closest('button');
        } else {
            btn = document.querySelector(`button[data-product-id="${productId}"]`) || 
                  document.querySelector(`button[onclick*="Favorites.toggle(${productId}"]`);
        }
        
        if (!btn) {
            console.error('Button not found for product:', productId);
            return;
        }
        
        const icon = btn.querySelector('i');
        if (!icon) {
            console.error('Icon not found in button');
            return;
        }
        
        const isFavorite = icon.classList.contains('bi-heart-fill');
        const originalText = btn.innerHTML;
        btn.disabled = true;
        
        try {
            const url = `/api/favorites/${productId}`;
            const method = isFavorite ? 'DELETE' : 'POST';
            
            const response = await fetch(url, {
                method: method,
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                }
            });
            
            const data = await response.json();
            
            if (response.ok && data.success) {
                if (isFavorite) {
                    icon.classList.remove('bi-heart-fill', 'text-danger');
                    icon.classList.add('bi-heart');
                    btn.classList.remove('btn-danger');
                    btn.classList.add('btn-outline-danger');
                    // Если на странице избранного, удаляем карточку
                    const card = btn.closest('.col-6, .col-md-4, .col-lg-3');
                    if (card && window.location.pathname === '/favorites') {
                        card.style.animation = 'fadeOut 0.3s ease';
                        setTimeout(() => card.remove(), 300);
                    }
                } else {
                    icon.classList.remove('bi-heart');
                    icon.classList.add('bi-heart-fill', 'text-danger');
                    btn.classList.remove('btn-outline-danger');
                    btn.classList.add('btn-danger');
                }
                    if (typeof Utils !== 'undefined' && Utils.showNotification) {
                        Utils.showNotification(data.message || 'Избранное обновлено', 'success');
                    } else {
                        alert(data.message || 'Избранное обновлено');
                    }
            } else {
                if (typeof Utils !== 'undefined' && Utils.showNotification) {
                    Utils.showNotification(data.message || 'Ошибка при обновлении избранного', 'danger');
                } else {
                    alert(data.message || 'Ошибка при обновлении избранного');
                }
            }
        } catch (error) {
            console.error('Favorites error:', error);
            if (typeof Utils !== 'undefined' && Utils.showNotification) {
                Utils.showNotification('Ошибка при обновлении избранного', 'danger');
            } else {
                alert('Ошибка при обновлении избранного');
            }
        } finally {
            btn.disabled = false;
        }
    },
    
    checkStatus: async function(productId) {
        try {
            const response = await fetch(`/api/favorites/${productId}`);
            if (response.ok) {
                const data = await response.json();
                const btn = document.querySelector(`button[data-product-id="${productId}"]`);
                if (btn && data.isFavorite) {
                    const icon = btn.querySelector('i');
                    icon.classList.remove('bi-heart');
                    icon.classList.add('bi-heart-fill', 'text-danger');
                    btn.classList.remove('btn-outline-danger');
                    btn.classList.add('btn-danger');
                }
            }
        } catch (error) {
            console.error('Error checking favorite status:', error);
        }
    }
};
