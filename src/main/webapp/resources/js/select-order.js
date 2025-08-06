$(document).ready(function () {
  $('#mergeSelectBtn').on('click', function () {
    const selected = $('.order-checkbox:checked');
    if (selected.length < 1) {
      alert('최소 1건 이상 선택해주세요.');
      return;
    }

    const baseProductId = selected.first().data('product-id');
    const isSameProduct = [...selected].every(cb =>
      $(cb).data('product-id') === baseProductId
    );

    if (!isSameProduct) {
      alert('동일한 제품만 선택 가능합니다.');
      return;
    }

    const orderList = [];
    let totalQty = 0;
    let earliestDueDate = null;

    selected.each(function () {
      const $cb = $(this);
      const clOrderId = $cb.data('cl-order-id');
      const orderQty = parseInt($cb.data('order-qty'));
      const dueDate = new Date($cb.data('due-date'));

      totalQty += orderQty;
      orderList.push(clOrderId);

      if (!earliestDueDate || dueDate < earliestDueDate) {
        earliestDueDate = dueDate;
      }
    });

    const formattedDate = earliestDueDate.toISOString().slice(0, 10);

    const mergedOrderData = {
      clOrderIds: orderList,
      productId: baseProductId,
      orderQty: totalQty,
      dueDate: formattedDate
    };

    if (window.opener && typeof window.opener.receiveOrderData === 'function') {
      window.opener.receiveOrderData(mergedOrderData);
      window.close();
    } else {
      alert('부모창과의 연결에 실패했습니다.');
    }
  });
});
