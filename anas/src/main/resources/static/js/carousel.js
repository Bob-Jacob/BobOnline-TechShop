// Carousel functionality
const Carousel = {
    init: function() {
        const carousels = document.querySelectorAll('.carousel');
        carousels.forEach(carousel => {
            const items = carousel.querySelectorAll('.carousel-item');
            if (items.length > 0) {
                // Проверяем, не инициализирована ли уже карусель
                const existingCarousel = bootstrap.Carousel.getInstance(carousel);
                if (!existingCarousel) {
                    try {
                        const bsCarousel = new bootstrap.Carousel(carousel, {
                            interval: 5000,
                            wrap: true,
                            pause: 'hover',
                            ride: 'carousel'
                        });
                    } catch (error) {
                        console.error('Error initializing carousel:', error);
                    }
                }
            }
        });
    }
};

// Tooltips initialization
const Tooltips = {
    init: function() {
        const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
        tooltipTriggerList.map(function (tooltipTriggerEl) {
            return new bootstrap.Tooltip(tooltipTriggerEl);
        });
    }
};

// Popovers initialization
const Popovers = {
    init: function() {
        const popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
        popoverTriggerList.map(function (popoverTriggerEl) {
            return new bootstrap.Popover(popoverTriggerEl);
        });
    }
};

// Initialize on DOM ready
document.addEventListener('DOMContentLoaded', function() {
    Carousel.init();
    Tooltips.init();
    Popovers.init();
});
