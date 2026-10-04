// Tutorial/Onboarding system for first-time users
const Tutorial = {
    steps: [
        {
            element: '.navbar-brand',
            title: 'Добро пожаловать!',
            content: 'Это главная страница магазина. Давайте начнем знакомство.',
            position: 'bottom'
        },
        {
            element: '.product-card:first-child',
            title: 'Товары',
            content: 'Здесь отображаются доступные товары. Нажмите "Подробнее" чтобы увидеть детали.',
            position: 'top'
        },
        {
            element: 'a[href="/cart"]',
            title: 'Корзина',
            content: 'Здесь хранятся товары, которые вы хотите купить. Добавьте товар в корзину, затем перейдите сюда.',
            position: 'left'
        },
        {
            element: 'a[href="/products"]',
            title: 'Каталог',
            content: 'Здесь вы можете просмотреть все доступные товары и отфильтровать их по категориям.',
            position: 'bottom'
        }
    ],
    
    currentStep: 0,
    isActive: false,
    
    init: function() {
        // Check if user has seen tutorial
        if (localStorage.getItem('tutorial_completed') === 'true') {
            return;
        }
        
        // Check if user is authenticated - look for authenticated elements
        const isAuthenticated = document.querySelector('a[href="/cart"]') !== null || 
                               document.querySelector('a[href="/orders"]') !== null ||
                               document.querySelector('a[href="/favorites"]') !== null;
        if (!isAuthenticated) {
            return; // Show tutorial only for authenticated users
        }
        
        // Start tutorial after a short delay
        setTimeout(() => {
            this.start();
        }, 1500);
    },
    
    start: function() {
        this.isActive = true;
        this.currentStep = 0;
        this.showStep(this.currentStep);
    },
    
    showStep: function(stepIndex) {
        if (stepIndex >= this.steps.length) {
            this.complete();
            return;
        }
        
        const step = this.steps[stepIndex];
        const element = document.querySelector(step.element);
        
        if (!element) {
            // Skip if element not found
            this.currentStep++;
            this.showStep(this.currentStep);
            return;
        }
        
        // Create overlay
        this.createOverlay();
        
        // Highlight element
        this.highlightElement(element);
        
        // Show tooltip
        this.showTooltip(element, step, stepIndex);
    },
    
    createOverlay: function() {
        if (document.getElementById('tutorial-overlay')) {
            return;
        }
        
        const overlay = document.createElement('div');
        overlay.id = 'tutorial-overlay';
        overlay.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.5);
            z-index: 9998;
            pointer-events: none;
        `;
        document.body.appendChild(overlay);
    },
    
    highlightElement: function(element) {
        const rect = element.getBoundingClientRect();
        const highlight = document.createElement('div');
        highlight.id = 'tutorial-highlight';
        highlight.style.cssText = `
            position: fixed;
            top: ${rect.top}px;
            left: ${rect.left}px;
            width: ${rect.width}px;
            height: ${rect.height}px;
            border: 3px solid #6366f1;
            border-radius: 8px;
            box-shadow: 0 0 0 9999px rgba(0, 0, 0, 0.5), 0 0 20px rgba(99, 102, 241, 0.5);
            z-index: 9999;
            pointer-events: none;
            transition: all 0.3s ease;
        `;
        document.body.appendChild(highlight);
        
        // Scroll to element
        element.scrollIntoView({ behavior: 'smooth', block: 'center' });
    },
    
    showTooltip: function(element, step, stepIndex) {
        const tooltip = document.createElement('div');
        tooltip.id = 'tutorial-tooltip';
        tooltip.className = 'tutorial-tooltip';
        
        const rect = element.getBoundingClientRect();
        let top = rect.bottom + 20;
        let left = rect.left + (rect.width / 2);
        
        if (step.position === 'top') {
            top = rect.top - 150;
        } else if (step.position === 'left') {
            top = rect.top + (rect.height / 2) - 75;
            left = rect.left - 250;
        } else if (step.position === 'right') {
            top = rect.top + (rect.height / 2) - 75;
            left = rect.right + 20;
        }
        
        tooltip.style.cssText = `
            position: fixed;
            top: ${top}px;
            left: ${left}px;
            transform: translateX(-50%);
            background: white;
            padding: 20px;
            border-radius: 12px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.3);
            z-index: 10000;
            max-width: 300px;
            animation: slideIn 0.3s ease;
        `;
        
        tooltip.innerHTML = `
            <div class="d-flex justify-content-between align-items-start mb-2">
                <h5 class="mb-0">${step.title}</h5>
                <button class="btn-close" onclick="Tutorial.skip()"></button>
            </div>
            <p class="mb-3">${step.content}</p>
            <div class="d-flex justify-content-between align-items-center">
                <small class="text-muted">Шаг ${stepIndex + 1} из ${this.steps.length}</small>
                <div>
                    ${stepIndex > 0 ? '<button class="btn btn-sm btn-outline-secondary me-2" onclick="Tutorial.prev()">Назад</button>' : ''}
                    <button class="btn btn-sm btn-primary" onclick="Tutorial.next()">
                        ${stepIndex === this.steps.length - 1 ? 'Завершить' : 'Далее'}
                    </button>
                </div>
            </div>
        `;
        
        document.body.appendChild(tooltip);
        
        // Add animation (use unique variable to avoid conflicts with other scripts)
        if (!document.getElementById('tutorial-styles')) {
            const tutorialStyle = document.createElement('style');
            tutorialStyle.id = 'tutorial-styles';
            tutorialStyle.textContent = `
                @keyframes slideIn {
                    from {
                        opacity: 0;
                        transform: translateX(-50%) translateY(-10px);
                    }
                    to {
                        opacity: 1;
                        transform: translateX(-50%) translateY(0);
                    }
                }
            `;
            document.head.appendChild(tutorialStyle);
        }
    },
    
    next: function() {
        this.cleanup();
        this.currentStep++;
        this.showStep(this.currentStep);
    },
    
    prev: function() {
        this.cleanup();
        this.currentStep--;
        if (this.currentStep < 0) this.currentStep = 0;
        this.showStep(this.currentStep);
    },
    
    skip: function() {
        this.complete();
    },
    
    cleanup: function() {
        const highlight = document.getElementById('tutorial-highlight');
        const tooltip = document.getElementById('tutorial-tooltip');
        if (highlight) highlight.remove();
        if (tooltip) tooltip.remove();
    },
    
    complete: function() {
        this.cleanup();
        const overlay = document.getElementById('tutorial-overlay');
        if (overlay) overlay.remove();
        localStorage.setItem('tutorial_completed', 'true');
        this.isActive = false;
        
        // Show completion message
        if (typeof Utils !== 'undefined') {
            Utils.showNotification('Обучение завершено! Теперь вы знаете, как пользоваться магазином.', 'success');
        }
    }
};

// Initialize tutorial on page load
document.addEventListener('DOMContentLoaded', function() {
    Tutorial.init();
});
