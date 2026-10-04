// Animations and interactive elements
const Animations = {
    // Animated counter
    animateCounter: function(element, target, duration = 2000) {
        let start = 0;
        const increment = target / (duration / 16);
        const timer = setInterval(() => {
            start += increment;
            if (start >= target) {
                element.textContent = target;
                clearInterval(timer);
            } else {
                element.textContent = Math.floor(start);
            }
        }, 16);
    },

    // Fade in on scroll
    initScrollAnimations: function() {
        // Проверяем поддержку IntersectionObserver
        if (typeof IntersectionObserver === 'undefined') {
            // Если не поддерживается, просто показываем все элементы
            document.querySelectorAll('.fade-in-on-scroll').forEach(el => {
                el.style.opacity = '1';
                el.style.transform = 'translateY(0)';
            });
            return;
        }

        const observerOptions = {
            threshold: 0.01, // Уменьшили порог для более раннего срабатывания
            rootMargin: '50px 0px -50px 0px' // Увеличили верхний отступ
        };

        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.style.opacity = '1';
                    entry.target.style.transform = 'translateY(0)';
                    observer.unobserve(entry.target);
                }
            });
        }, observerOptions);

        document.querySelectorAll('.fade-in-on-scroll').forEach(el => {
            // Проверяем, виден ли элемент уже при загрузке
            const rect = el.getBoundingClientRect();
            const isVisible = rect.top < window.innerHeight && rect.bottom > 0;
            
            if (isVisible) {
                // Если элемент уже виден, показываем его сразу
                el.style.opacity = '1';
                el.style.transform = 'translateY(0)';
            } else {
                // Если элемент не виден, устанавливаем начальное скрытое состояние
                el.style.opacity = '0';
                el.style.transform = 'translateY(20px)';
                el.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
                observer.observe(el);
            }
        });
    },

    // Parallax effect for hero section
    initParallax: function() {
        const hero = document.querySelector('.hero-section');
        if (!hero) return;

        window.addEventListener('scroll', () => {
            const scrolled = window.pageYOffset;
            const rate = scrolled * 0.5;
            hero.style.transform = `translateY(${rate}px)`;
        });
    },

    // Floating animation for icons
    initFloatingIcons: function() {
        document.querySelectorAll('.floating-icon').forEach((icon, index) => {
            icon.style.animation = `float 3s ease-in-out infinite`;
            icon.style.animationDelay = `${index * 0.2}s`;
        });
    },

    // Typewriter effect
    typewriter: function(element, text, speed = 100) {
        let i = 0;
        element.textContent = '';
        const timer = setInterval(() => {
            if (i < text.length) {
                element.textContent += text.charAt(i);
                i++;
            } else {
                clearInterval(timer);
            }
        }, speed);
    },

    // Pulse animation for buttons
    initPulseButtons: function() {
        document.querySelectorAll('.pulse-on-hover').forEach(btn => {
            btn.addEventListener('mouseenter', function() {
                this.style.animation = 'pulse 1s ease-in-out infinite';
            });
            btn.addEventListener('mouseleave', function() {
                this.style.animation = '';
            });
        });
    }
};

// Add CSS animations (use unique variable name to avoid conflicts)
const animationsStyle = document.createElement('style');
animationsStyle.textContent = `
    @keyframes float {
        0%, 100% { transform: translateY(0px); }
        50% { transform: translateY(-20px); }
    }
    
    @keyframes pulse {
        0%, 100% { transform: scale(1); }
        50% { transform: scale(1.05); }
    }
    
    .fade-in-on-scroll {
        /* Не устанавливаем opacity: 0 по умолчанию в CSS, 
           чтобы элементы были видны до применения JavaScript */
        transition: opacity 0.6s ease, transform 0.6s ease;
    }
    
    .product-card {
        transition: transform 0.3s ease, box-shadow 0.3s ease;
    }
    
    .product-card:hover {
        transform: translateY(-5px);
        box-shadow: 0 10px 25px rgba(0,0,0,0.15);
    }
    
    .info-banner {
        transition: all 0.3s ease;
    }
    
    .info-banner:hover i {
        animation: float 2s ease-in-out infinite;
    }
`;
document.head.appendChild(animationsStyle);

// Initialize on DOM ready
document.addEventListener('DOMContentLoaded', function() {
    // Сначала добавляем классы к элементам
    document.querySelectorAll('.info-banner').forEach((banner, index) => {
        banner.classList.add('fade-in-on-scroll');
        banner.style.transitionDelay = `${index * 0.15}s`;
    });
    
    // Add fade-in class to product cards
    document.querySelectorAll('.product-card').forEach((card, index) => {
        if (!card.classList.contains('fade-in-on-scroll')) {
            card.classList.add('fade-in-on-scroll');
            card.style.transitionDelay = `${index * 0.1}s`;
        }
    });
    
    // Затем инициализируем анимации (с небольшой задержкой для правильной работы IntersectionObserver)
    setTimeout(() => {
        Animations.initScrollAnimations();
    }, 50);
    
    Animations.initFloatingIcons();
    Animations.initPulseButtons();
});
