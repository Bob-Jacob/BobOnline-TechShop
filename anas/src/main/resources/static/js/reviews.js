// Reviews functionality
const Reviews = {
    setRating: function(rating) {
        const ratingInput = document.getElementById('rating-value');
        if (!ratingInput) return;
        
        ratingInput.value = rating;
        const stars = document.querySelectorAll('.rating-input i[data-rating]');
        stars.forEach((star) => {
            const starRating = parseInt(star.getAttribute('data-rating'));
            if (starRating <= rating) {
                star.classList.remove('bi-star');
                star.classList.add('bi-star-fill', 'text-warning');
            } else {
                star.classList.remove('bi-star-fill', 'text-warning');
                star.classList.add('bi-star');
            }
        });
    },
    
    addReview: async function(productId, form) {
        if (!productId) {
            console.error('Product ID is required');
            if (typeof Utils !== 'undefined' && Utils.showNotification) {
                Utils.showNotification('Ошибка: ID товара не найден', 'danger');
            }
            return;
        }
        
        const ratingInput = form.querySelector('#rating-value');
        const commentInput = form.querySelector('#review-comment');
        
        if (!ratingInput) {
            console.error('Rating input not found');
            if (typeof Utils !== 'undefined' && Utils.showNotification) {
                Utils.showNotification('Ошибка: поле рейтинга не найдено', 'danger');
            }
            return;
        }
        
        const rating = parseInt(ratingInput.value);
        const comment = commentInput ? commentInput.value.trim() : '';
        
        if (!rating || rating < 1 || rating > 5) {
            if (typeof Utils !== 'undefined' && Utils.showNotification) {
                Utils.showNotification('Выберите рейтинг от 1 до 5', 'warning');
            }
            return;
        }
        
        try {
            const formData = new FormData();
            formData.append('rating', rating);
            if (comment) {
                formData.append('comment', comment);
            }
            
            const response = await fetch(`/product/${productId}/review`, {
                method: 'POST',
                body: formData,
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                }
            });
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const data = await response.json();
            
            if (data.success) {
                if (typeof Utils !== 'undefined' && Utils.showNotification) {
                    Utils.showNotification(data.message || 'Отзыв добавлен и ожидает модерации', 'success');
                }
                form.reset();
                Reviews.setRating(5);
                setTimeout(() => location.reload(), 1500);
            } else {
                if (typeof Utils !== 'undefined' && Utils.showNotification) {
                    Utils.showNotification(data.message || 'Ошибка при добавлении отзыва', 'danger');
                }
            }
        } catch (error) {
            console.error('Error adding review:', error);
            if (typeof Utils !== 'undefined' && Utils.showNotification) {
                Utils.showNotification('Ошибка при добавлении отзыва: ' + error.message, 'danger');
            }
        }
    }
};
