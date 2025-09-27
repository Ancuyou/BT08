let products = [];
let categoriesForSelect = [];
let currentId = null;

let productModalEl, productModal;
let deleteModalEl, deleteModal;

$(document).ready(function () {
    if (typeof $ === 'undefined') console.error('jQuery required');
    if (typeof bootstrap === 'undefined') console.error('Bootstrap JS required');

    productModalEl = document.getElementById('productModal');
    deleteModalEl = document.getElementById('deleteModal');

    if (productModalEl && typeof bootstrap !== 'undefined') {
        productModal = new bootstrap.Modal(productModalEl, { backdrop: 'static' });
        productModalEl.addEventListener('hidden.bs.modal', function () {
            const form = $('#productForm')[0];
            if (form) form.reset();
            $('#imagePreview').hide().attr('src', '');
            $('#productId').val('');
            $('#alert-container').empty();
        });
    }
    if (deleteModalEl && typeof bootstrap !== 'undefined') {
        deleteModal = new bootstrap.Modal(deleteModalEl);
        deleteModalEl.addEventListener('hidden.bs.modal', function () { currentId = null; });
    }

    $("#productForm").on('submit', function (e) {
        e.preventDefault();
        saveProduct();
    });

    $("#images").on('change', function () { previewImage(this, '#imagePreview'); });
    $("#confirmDeleteBtn").on('click', function () { deleteProduct(currentId); });

    $(document).on('click', '.btn-edit-product', function () {
        const id = $(this).data('id');
        openEditModal(id);
    });
    $(document).on('click', '.btn-delete-product', function () {
        const id = $(this).data('id');
        const name = $(this).data('name');
        openDeleteModal(id, name);
    });

    loadProducts();
    loadCategoriesForSelect();
});

/* Helpers */
function escapeHtml(text) {
    if (text === null || text === undefined) return '';
    return String(text).replace(/[&<>"']/g, function (m) {
        return ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'})[m];
    });
}
function escapeAttr(text) { return escapeHtml(text).replace(/"/g,'&quot;'); }

/* Load products */
function loadProducts() {
    $.get("/api/product")
        .done(function (res) {
            if (res && (res.status === true || res.success === true || Array.isArray(res))) {
                products = res.body || res.data || res;
                renderTable(products);
            } else if (res && Array.isArray(res.body)) {
                products = res.body;
                renderTable(products);
            } else {
                $('#alert-container').html(`<div class="alert alert-danger">${escapeHtml(res && res.message ? res.message : 'Không có dữ liệu')}</div>`);
                renderTable([]);
            }
        })
        .fail(function (err) {
            console.error("Lỗi khi load products:", err);
            $('#alert-container').html('<div class="alert alert-danger">Không thể load dữ liệu</div>');
            renderTable([]);
        });
}

function renderTable(data) {
    const tbody = $("#productTableBody");
    tbody.empty();
    if (!Array.isArray(data) || data.length === 0) {
        $("#emptyState").show();
        return;
    }
    $("#emptyState").hide();

    data.forEach(p => {
        const id = escapeHtml(p.productId);
        const name = escapeHtml(p.productName);
        const imgHtml = p.images ? `<img src="/uploads/${escapeAttr(p.images)}" style="max-height:50px;">` : '';
        const catName = p.category ? escapeHtml(p.category.categoryName) : '';
        const row = `
            <tr>
                <td>${id}</td>
                <td>${name}</td>
                <td>${imgHtml}</td>
                <td>${catName}</td>
                <td>${escapeHtml(p.quantity)}</td>
                <td>${escapeHtml(p.unitPrice)}</td>
                <td>${escapeHtml(p.discount)}</td>
                <td>${escapeHtml(p.status)}</td>
                <td>
                    <button class="btn btn-warning btn-sm btn-edit-product" data-id="${id}">Edit</button>
                    <button class="btn btn-danger btn-sm btn-delete-product" data-id="${id}" data-name="${escapeAttr(p.productName)}">Delete</button>
                </td>
            </tr>
        `;
        tbody.append(row);
    });
}

/* Search */
function searchProducts() {
    const term = $("#searchInput").val()?.toLowerCase() || "";
    const filtered = products.filter(p => (p.productName || "").toLowerCase().includes(term));
    renderTable(filtered);
}

/* Categories for select */
function loadCategoriesForSelect() {
    $.get("/api/category")
        .done(function (res) {
            let list = res.body || res.data || res;
            categoriesForSelect = Array.isArray(list) ? list : [];
            const sel = $("#categoryId");
            sel.empty();
            sel.append(`<option value="">-- No Category --</option>`);
            categoriesForSelect.forEach(c => {
                sel.append(`<option value="${escapeAttr(c.categoryId)}">${escapeHtml(c.categoryName)}</option>`);
            });
        })
        .fail(function (err) {
            console.error("Không thể load categories for select", err);
        });
}

/* Modal actions */
function openAddModal() {
    $('#productId').val('');
    $('#productName').val('');
    $('#quantity').val(0);
    $('#unitPrice').val(0);
    $('#discount').val(0);
    $('#status').val('1');
    $('#description').val('');
    $('#images').val('');
    $('#imagePreview').hide();
    $('#productModalTitle').text('Add Product');
    if (productModal) productModal.show();
}

function openEditModal(id) {
    const p = products.find(x => String(x.productId) === String(id));
    if (!p) return;
    $('#productId').val(p.productId);
    $('#productName').val(p.productName);
    $('#quantity').val(p.quantity);
    $('#unitPrice').val(p.unitPrice);
    $('#discount').val(p.discount);
    $('#status').val(p.status);
    $('#description').val(p.description || '');
    if (p.images) {
        $('#imagePreview').attr('src', '/uploads/' + p.images).show();
    } else {
        $('#imagePreview').hide();
    }
    $('#categoryId').val(p.category ? p.category.categoryId : '');
    $('#productModalTitle').text('Edit Product');
    if (productModal) productModal.show();
}

/* Save product */
function saveProduct() {
    const form = $('#productForm')[0];
    if (!form) return;
    const fd = new FormData(form);
    const id = $('#productId').val();
    const url = id ? "/api/product/updateProduct" : "/api/product/addProduct";
    // NOTE: backend expects PUT for update in controller; here using POST/PUT like your Category code uses POST. We follow Category pattern (POST)
    const method = id ? 'PUT' : 'POST';

    // Some servers don't accept PUT with multipart easily. If you use PUT in controller, jQuery should send with processData=false contentType=false.
    $.ajax({
        url: url,
        type: method,
        data: fd,
        processData: false,
        contentType: false,
        success: function (res) {
            if (res && (res.status === true || res.success === true)) {
                if (productModal) productModal.hide();
                loadProducts();
                $('#alert-container').html(`<div class="alert alert-success">${escapeHtml(res.message || 'Lưu thành công')}</div>`);
            } else {
                $('#alert-container').html(`<div class="alert alert-danger">${escapeHtml(res && res.message ? res.message : 'Lưu thất bại')}</div>`);
            }
        },
        error: function (err) {
            console.error("Lỗi khi lưu product:", err);
            $('#alert-container').html('<div class="alert alert-danger">Không thể lưu Product</div>');
        }
    });
}

/* Delete */
function openDeleteModal(id, name) {
    currentId = id;
    $('#deleteName').text(name);
    if (deleteModal) deleteModal.show();
}

function deleteProduct(id) {
    if (!id) return;
    $.ajax({
        url: "/api/product/deleteProduct?productId=" + encodeURIComponent(id),
        type: "DELETE",
        success: function (res) {
            if (res && (res.status === true || res.success === true)) {
                if (deleteModal) deleteModal.hide();
                loadProducts();
                $('#alert-container').html(`<div class="alert alert-success">${escapeHtml(res.message || 'Xóa thành công')}</div>`);
            } else {
                $('#alert-container').html(`<div class="alert alert-danger">${escapeHtml(res && res.message ? res.message : 'Xóa thất bại')}</div>`);
            }
        },
        error: function (err) {
            console.error("Lỗi khi xóa product:", err);
            $('#alert-container').html('<div class="alert alert-danger">Không thể xóa Product</div>');
        }
    });
}

/* Preview image helper */
function previewImage(input, previewSelector) {
    if (input.files && input.files[0]) {
        const reader = new FileReader();
        reader.onload = function (e) {
            $(previewSelector).attr('src', e.target.result).show();
        };
        reader.readAsDataURL(input.files[0]);
    } else {
        $(previewSelector).hide().attr('src','');
    }
}
