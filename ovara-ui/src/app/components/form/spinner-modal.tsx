import React from 'react';
import { FullSpinner } from '@/app/components/full-spinner';
import { Modal } from '@mui/material';

export function SpinnerModal({ open }: { open: boolean }) {
  return (
    <Modal
      open={open}
      aria-labelledby="child-modal-title"
      aria-describedby="child-modal-description"
    >
      <FullSpinner />
    </Modal>
  );
}
